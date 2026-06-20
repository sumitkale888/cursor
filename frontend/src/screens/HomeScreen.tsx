import React, {useCallback, useEffect, useMemo, useState} from 'react';
import {
  Alert,
  FlatList,
  RefreshControl,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import {CompositeNavigationProp} from '@react-navigation/native';
import {BottomTabNavigationProp} from '@react-navigation/bottom-tabs';
import {NativeStackNavigationProp} from '@react-navigation/native-stack';
import {parseApiError} from '../api/client';
import * as productApi from '../api/productApi';
import {CategoryCard} from '../components/CategoryCard';
import {ErrorView} from '../components/ErrorView';
import {Header} from '../components/Header';
import {LoadingSpinner} from '../components/LoadingSpinner';
import {ProductCard} from '../components/ProductCard';
import {SearchBar} from '../components/SearchBar';
import {useAuth} from '../context/AuthContext';
import {useCart} from '../context/CartContext';
import {HomeStackParamList, MainTabParamList} from '../navigation/types';
import {Category, Product} from '../types';
import {colors} from '../theme/colors';
import {spacing} from '../theme/spacing';

type HomeNav = CompositeNavigationProp<
  NativeStackNavigationProp<HomeStackParamList, 'Home'>,
  BottomTabNavigationProp<MainTabParamList>
>;

type Props = {
  navigation: HomeNav;
};

export function HomeScreen({navigation}: Props) {
  const {user} = useAuth();
  const {addItem, refreshCart} = useCart();
  const [products, setProducts] = useState<Product[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const categories = useMemo(
    () => productApi.extractCategories(products),
    [products],
  );

  const displayedProducts = products;

  const loadProducts = useCallback(async (query?: string) => {
    setError(null);
    try {
      const data = query?.trim()
        ? await productApi.searchProducts(query.trim())
        : await productApi.getAllProducts();
      setProducts(data);
    } catch (err) {
      setError(parseApiError(err).message);
    }
  }, []);

  useEffect(() => {
    async function init() {
      setLoading(true);
      await loadProducts();
      await refreshCart();
      setLoading(false);
    }
    init();
  }, [loadProducts, refreshCart]);

  const onRefresh = async () => {
    setRefreshing(true);
    await loadProducts();
    await refreshCart();
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

  if (error && products.length === 0) {
    return <ErrorView message={error} onRetry={loadProducts} />;
  }

  const renderHeader = () => (
    <>
      <SearchBar
        value={searchQuery}
        onChangeText={setSearchQuery}
        onSubmitEditing={() => loadProducts(searchQuery)}
        onClear={() => {
          setSearchQuery('');
          loadProducts();
        }}
      />
      {categories.length > 0 && !searchQuery ? (
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Shop by Category</Text>
          <View style={styles.categoryGrid}>
            {categories.slice(0, 4).map((cat: Category) => (
              <CategoryCard
                key={cat.id}
                category={cat}
                onPress={() =>
                  navigation.navigate('CategoriesTab', {
                    screen: 'CategoryProducts',
                    params: {categoryId: cat.id, categoryName: cat.name},
                  })
                }
              />
            ))}
          </View>
        </View>
      ) : null}
      <Text style={styles.sectionTitle}>
        {searchQuery ? 'Search Results' : 'All Products'}
      </Text>
    </>
  );

  return (
    <View style={styles.container}>
      <Header
        title="blinkit"
        subtitle={`Hi, ${user?.firstName ?? 'there'} 👋`}
      />
      <FlatList
        data={displayedProducts}
        keyExtractor={item => String(item.id)}
        numColumns={2}
        ListHeaderComponent={renderHeader}
        ListEmptyComponent={
          <Text style={styles.empty}>No products found</Text>
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
        contentContainerStyle={styles.list}
        refreshControl={
          <RefreshControl
            refreshing={refreshing}
            onRefresh={onRefresh}
            colors={[colors.primary]}
          />
        }
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
  section: {
    marginBottom: spacing.md,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: colors.text,
    marginHorizontal: spacing.lg,
    marginBottom: spacing.md,
    marginTop: spacing.sm,
  },
  categoryGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    paddingHorizontal: spacing.sm,
  },
  empty: {
    textAlign: 'center',
    color: colors.textSecondary,
    marginTop: spacing.xxxl,
    fontSize: 15,
  },
});
