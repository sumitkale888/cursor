import React, {useCallback, useEffect, useState} from 'react';
import {
  Alert,
  FlatList,
  RefreshControl,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import {NativeStackScreenProps} from '@react-navigation/native-stack';
import {parseApiError} from '../api/client';
import * as productApi from '../api/productApi';
import {ErrorView} from '../components/ErrorView';
import {Header} from '../components/Header';
import {LoadingSpinner} from '../components/LoadingSpinner';
import {ProductCard} from '../components/ProductCard';
import {useCart} from '../context/CartContext';
import {CategoriesStackParamList} from '../navigation/types';
import {Product} from '../types';
import {colors} from '../theme/colors';
import {spacing} from '../theme/spacing';

type Props = NativeStackScreenProps<
  CategoriesStackParamList,
  'CategoryProducts'
>;

export function CategoryProductsScreen({navigation, route}: Props) {
  const {categoryId, categoryName} = route.params;
  const {addItem} = useCart();
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadProducts = useCallback(async () => {
    setError(null);
    try {
      const data = await productApi.getProductsByCategory(categoryId);
      setProducts(data);
    } catch (err) {
      setError(parseApiError(err).message);
    }
  }, [categoryId]);

  useEffect(() => {
    async function init() {
      setLoading(true);
      await loadProducts();
      setLoading(false);
    }
    init();
  }, [loadProducts]);

  const onRefresh = async () => {
    setRefreshing(true);
    await loadProducts();
    setRefreshing(false);
  };

  const handleAddToCart = async (productId: number) => {
    try {
      await addItem(productId, 1);
      Alert.alert('Added', 'Item added to cart');
    } catch (err) {
      Alert.alert('Error', (err as Error).message);
    }
  };

  if (loading) {
    return <LoadingSpinner fullScreen />;
  }

  if (error) {
    return <ErrorView message={error} onRetry={loadProducts} />;
  }

  return (
    <View style={styles.container}>
      <Header
        title={categoryName}
        subtitle={`${products.length} products`}
        showBack
        onBack={() => navigation.goBack()}
      />
      <FlatList
        data={products}
        keyExtractor={item => String(item.id)}
        numColumns={2}
        contentContainerStyle={styles.list}
        ListEmptyComponent={
          <Text style={styles.empty}>No products in this category</Text>
        }
        refreshControl={
          <RefreshControl
            refreshing={refreshing}
            onRefresh={onRefresh}
            colors={[colors.primary]}
          />
        }
        renderItem={({item}) => (
          <ProductCard
            product={item}
            compact
            onPress={() =>
              navigation.navigate('ProductDetail', {productId: item.id})
            }
            onAddToCart={() => handleAddToCart(item.id)}
          />
        )}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  list: {
    paddingBottom: spacing.xxl,
  },
  empty: {
    textAlign: 'center',
    color: colors.textSecondary,
    marginTop: spacing.xxxl,
  },
});
